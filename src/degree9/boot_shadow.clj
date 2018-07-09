(ns degree9.boot-shadow
  (:require [boot.core :as boot]
            [boot.util :as util]
            [shadow.cljs.devtools.server :as server]))

(defn sync-fileset! [fileset temp]
  (apply boot/sync! temp (boot/output-dirs fileset)))

(defn start-server! []
  (util/info "Starting shadow-cljs server...\n")
  (server/start!))

(defn restart-server! []
  (util/info "Restarting shadow-cljs server...\n")
  (server/stop!))

(defn stop-server! []
  (util/info "Stopping shadow-cljs server...\n")
  (server/stop!))

(boot/deftask server
  "Start or Restart a shadow-cljs server."
  []
  (let [tmp (boot/tmp-dir!)]
    (start-server!)
    (boot/cleanup (stop-server!))
    (boot/with-pass-thru fileset
      (sync-fileset! fileset tmp)
      (restart-server!))))
