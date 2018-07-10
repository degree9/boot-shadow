(ns degree9.boot-shadow.impl
  (:require [clojure.java.io :as io]
            [boot.core :as boot]
            [boot.pod  :as pod]
            [boot.util :as util]))

;; Helper Macros ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmacro require-in-pod [pod & body]
  `(pod/with-eval-in ~pod
    (require ~@body)))

(defmacro eval-in-pod [pod & body]
  `(pod/with-eval-in ~pod
    (shadow.cljs.devtools.api/with-runtime
      (try ~@body
        (catch Exception e#
          (shadow.cljs.devtools.errors/user-friendly-error e#))))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Helper Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def shadow-config "./shadow-cljs.edn")

(defn- ensure-shadow! []
  (when-not (.exists (io/file shadow-config))
    (util/warn "The shadow-cljs file is missing...: %s\n" shadow-config)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Shadow-CLJS Pod ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def shadow-pod
  (pod/make-pod
    (update-in pod/env [:dependencies] into
      (-> "degree9/boot_shadow/pod_deps.edn" io/resource slurp read-string))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Shadow-CLJS Embedded Server ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- stop-server! [pod]
  (boot.pod/with-eval-in pod
    (when (shadow.cljs.devtools.server.runtime/get-instance)
      (boot.util/info "Stopping shadow-cljs server...\n")
      (shadow.cljs.devtools.server/stop!))))

(defn- start-server! [pod]
  (boot.pod/with-eval-in pod
    (when-not (shadow.cljs.devtools.server.runtime/get-instance)
      (boot.util/info "Starting shadow-cljs server...\n")
      (shadow.cljs.devtools.server/start!))))

(defn server-impl [*opts*]
  (ensure-shadow!)
  (require-in-pod shadow-pod
    '[shadow.cljs.devtools.server]
    '[shadow.cljs.devtools.server.runtime])
  (boot/with-pass-thru fileset
    (stop-server! shadow-pod)
    (start-server! shadow-pod)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;; Shadow-CLJS Compile ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn compile! [pod build output cache]
  (eval-in-pod pod
    (let [{:keys [output-dir] :as config} (shadow.cljs.devtools.api/get-build-config ~build)
          target (.getAbsolutePath (clojure.java.io/file ~output output-dir))
          config (assoc config :cache-root ~cache :output-dir target)]
      (boot.util/info "Compiling ClojureScript using shadow-cljs...: %s\n" ~build)
      (shadow.cljs.devtools.api/compile* config {}))))

(defn compile-impl [*opts*]
  (let [build     (:build  *opts* :app)
        tmp       (boot/tmp-dir!)
        tmp-dir   (.getAbsolutePath tmp)
        cache     (boot/cache-dir! ::cache)
        cache-dir (.getAbsolutePath cache)]
    (ensure-shadow!)
    (require-in-pod shadow-pod
      '[shadow.cljs.devtools.api]
      '[shadow.cljs.devtools.errors])
    (boot/with-pre-wrap fileset
      (compile! shadow-pod build tmp-dir cache-dir)
      (-> fileset (boot/add-resource tmp) boot/commit!))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
