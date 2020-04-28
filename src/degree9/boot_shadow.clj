(ns degree9.boot-shadow
  (:refer-clojure :exclude [compile])
  (:require [boot.core :as boot]
            [degree9.boot-shadow.impl :as impl]))

(boot/deftask server
  "Start or Restart the shadow-cljs embedded server."
  []
  (impl/server-impl *opts*))

(boot/deftask compile
  "Compile a ClojureScript project using shadow-cljs."
  [b build  VAR kw  "Build ID. (:app)"]
  (impl/compile-impl *opts*))

(boot/deftask release
  "Release a ClojureScript project using shadow-cljs."
  [b build  VAR kw  "Build ID. (:app)"]
  (impl/release-impl *opts*))
