(set-env!
 :dependencies  '[[org.clojure/clojure                 "1.9.0"]
                  [boot/core                           "2.7.2"]
                  [degree9/boot-semver                 "1.7.0"  :scope "test"]
                  [thheller/shadow-cljs                "2.4.17" :scope "test"]]
 :resource-paths   #{"src"})

(require
 '[degree9.boot-semver :refer :all]
 '[degree9.boot-shadow :as shadow])

(task-options!
  pom    {:project 'degree9/boot-shadow
          :description "Boot-clj task for compiling ClojureScript using shadow-cljs."
          :url         "https://github.com/degree9/boot-shadow"
          :scm         {:url "https://github.com/degree9/boot-shadow"}}
  target {:dir #{"target"}})

(deftask develop
  "Build boot-shadow for development."
  []
  (comp
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (watch)
   (target)
   (build-jar)))

(deftask deploy
  "Build boot-shadow and deploy to clojars."
  []
  (comp
   (version)
   (target)
   (build-jar)
   (push-release)))
