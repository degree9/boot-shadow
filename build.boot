(set-env!
 :dependencies  '[[boot/core            "2.8.2"]
                  [degree9/boot-semver  "1.8.0"  :scope "test"]]
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
