(ns degree9.boot-shadow.pod
  (:require [clojure.java.io :as io]
            [boot.util :as util]
            [shadow.cljs.devtools.api :as api]
            [shadow.cljs.devtools.cli]
            [shadow.cljs.devtools.errors]
            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.server.runtime :as runtime]))

;; This namespace is for use within shadow-pod context only ;;;;;;;;;;;;;;;;;;;;

(defn- absolute-path [parent child]
  (.getAbsolutePath (io/file parent child)))

(defn- patch-config [config output cache]
  (let [out-path (partial absolute-path output)
        proj-path (partial absolute-path ".")]
    (cond-> config ;(assoc config :cache-root cache) ;; disable cache for now
      (:output-dir config)  (update :output-dir out-path)
      (:output-to config)   (update :output-to  out-path)
      (:http-root (:devtools config)) (update-in [:devtools :http-root] out-path)
      (:js-package-dirs (:js-options config)) (update-in [:js-options :js-package-dirs] (partial map proj-path)))))

(defn get-config [build]
  (api/get-build-config build))

;;;;;;

(defn- server-msg [action]
  (util/info "%s shadow-cljs server...\n" action))

(defn- shadow-msg [action]
  (util/info "Compiling ClojureScript using shadow-cljs: %s\n" action))

(defn start! []
  (when-not (runtime/get-instance)
    (server-msg "Starting")
    (server/start!)))

(defn stop! []
  (when (runtime/get-instance)
    (server-msg "Stopping")
    (server/stop!)))

(defn compile! [build output cache]
  (let [config (patch-config (get-config build) output cache)]
    (prn config)
    (shadow-msg "compile")
    (api/compile* config {})))

(defn release! [build output cache]
  (let [config (patch-config (get-config build) output cache)]
    (shadow-msg "release")
    (api/release* config {})))
