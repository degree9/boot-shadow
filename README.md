# boot-shadow
[![Clojars Project](https://img.shields.io/clojars/v/degree9/boot-shadow.svg)](https://clojars.org/degree9/boot-shadow)
[![Dependencies Status](https://jarkeeper.com/degree9/boot-shadow/status.svg)](https://jarkeeper.com/degree9/boot-shadow)
[![Downloads](https://jarkeeper.com/degree9/boot-shadow/downloads.svg)](https://jarkeeper.com/degree9/boot-shadow)
<!---
[![CircleCI](https://circleci.com/gh/degree9/boot-shadow.svg?style=svg)](https://circleci.com/gh/degree9/boot-shadow)
--->

Boot-clj task for compiling ClojureScript using shadow-cljs.

---

<p align="center">
  <a href="https://degree9.io" align="center">
    <img width="135" src="http://degree9.io/images/degree9.png">
  </a>
  <br>
  <b>boot-shadow is developed and maintained by Degree9</b>
</p>

---

## Usage

Add `boot-shadow` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[degree9/boot-shadow "X.Y.Z" :scope "test"]])
(require '[degree9.boot-shadow :as shadow])
```

`boot-shadow` tasks follow a similar syntax to the shadow-cljs cli:

```bash
boot shadow/server

boot shadow/compile --build app

boot shadow/release --build app
```
