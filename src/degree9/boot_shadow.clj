(ns degree9.boot-shadow
  (:refer-clojure :exclude [compile])
  (:require [boot.core :as boot]
            [boot.util :as util]
            [degree9.boot-shadow.impl :as impl]))

(boot/deftask server
  "Start or Restart the shadow-cljs embedded server."
  []
  (impl/server-impl *opts*))

(boot/deftask compiler
  "Compile a ClojureScript project using shadow-cljs."
  [b build  VAR kw  "Build ID. (:app)"]
   ;c config VAR str "Path to shadow-cljs.edn file. (./shadow-cljs.edn)"]
  (impl/compile-impl *opts*))
