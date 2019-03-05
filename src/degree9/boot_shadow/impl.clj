(ns degree9.boot-shadow.impl
  (:require [clojure.java.io :as io]
            [boot.core :as boot]
            [boot.pod  :as pod]
            [boot.util :as util]))

;; Helper Macros ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmacro require-in [pod & body]
  `(pod/with-eval-in ~pod
    (require ~@body)))

(defmacro with-shadow-eval [pod & body]
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

(defn- fs-sync! [fileset tmp]
  (apply boot/sync! tmp (boot/input-dirs fileset)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Shadow-CLJS Pod ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def shadow-pod
  (delay
    (doto
      (pod/make-pod
        (-> (boot/get-env)
          (update-in [:dependencies] into (-> "degree9/boot_shadow/pod_deps.edn" io/resource slurp read-string))))
      (require-in '[degree9.boot-shadow.pod]))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Shadow-CLJS Embedded Server ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- stop-server! [pod]
  (boot.pod/with-call-in pod
    (degree9.boot-shadow.pod/stop!)))

(defn- start-server! [pod]
  (boot.pod/with-call-in pod
    (degree9.boot-shadow.pod/start!)))

(defn server-impl [*opts*]
  (ensure-shadow!)
  (boot/with-pass-thru fileset
    (stop-server!  @shadow-pod)
    (start-server! @shadow-pod)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;; Shadow-CLJS Compile ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn compile! [pod build output cache]
  (let [output (.getAbsolutePath output)
        cache  (.getAbsolutePath cache)]
    (with-shadow-eval pod
      (degree9.boot-shadow.pod/compile! ~build ~output ~cache))))

(defn compile-impl [*opts*]
  (let [build     (:build  *opts* :app)
        tmp       (boot/tmp-dir!)
        cache     (boot/cache-dir! ::cache)]
    (ensure-shadow!)
    (boot/with-pre-wrap fileset
      (fs-sync! fileset tmp)
      (compile! @shadow-pod build tmp cache)
      (-> fileset (boot/add-resource tmp) boot/commit!))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;; Shadow-CLJS Release ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn release! [pod build output cache]
  (let [output (.getAbsolutePath output)
        cache  (.getAbsolutePath cache)]
    (with-shadow-eval pod
      (degree9.boot-shadow.pod/release! ~build ~output ~cache))))

(defn release-impl [*opts*]
  (let [build     (:build  *opts* :app)
        tmp       (boot/tmp-dir!)
        cache     (boot/cache-dir! ::cache)]
    (ensure-shadow!)
    (boot/with-pre-wrap fileset
      (fs-sync! fileset tmp)
      (release! @shadow-pod build tmp cache)
      (-> fileset (boot/add-resource tmp) boot/commit!))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
