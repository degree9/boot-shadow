(ns degree9.boot-shadow.pod
  (:require [clojure.java.io :as io]
            [boot.util :as util]
            [shadow.cljs.devtools.api :as api]
            [shadow.cljs.devtools.errors]
            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.server.runtime :as runtime]))

;; This namespace is for use within shadow-pod context only ;;;;;;;;;;;;;;;;;;;;

(defn- absolute-path [parent child]
  (.getAbsolutePath (io/file parent child)))
  
(defn- patch-config [config output cache]
  (let [out-path (partial absolute-path output)]
    (-> config
      (assoc  :cache-root cache)
      (update :output-dir out-path)
      (update :output-to  out-path)
      (update-in [:devtools :http-root] out-path))))

(defn get-config [build]
  (api/get-build-config build))

;;;;;;

(defn start! []
  (when-not (runtime/get-instance)
    (util/info "Starting shadow-cljs server...\n")
    (server/start!)))

(defn stop! []
  (when (runtime/get-instance)
    (util/info "Stopping shadow-cljs server...\n")
    (server/stop!)))

(defn compile! [build output cache]
  (let [config (patch-config (get-config build) output cache)]
    (util/info "Compiling ClojureScript using shadow-cljs...: %s\n" build)
    (api/compile* config {})))
