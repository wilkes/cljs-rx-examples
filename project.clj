(defproject cljs-rx-examples "0.1.0-SNAPSHOT"
  :description "Messing around with RxJS and ClojureScript"
  :url "https://github.com/wilkes/cljs-rx-examples"
  :dependencies [[clj-http "0.5.7"]
                 [noir "1.3.0-beta10"]
                 [jayq "1.0.0"]
                 [fetch "0.1.0-alpha2"]
                 [crate "0.2.3"]
                 [org.clojure/clojure "1.4.0"]
                 [clj-time "0.4.4"]
                 [lib-noir "0.2.0-alpha2"]]
  :plugins [[lein-cljsbuild "0.2.9"]]
  :cljsbuild {:builds
              {:debug {:source-path "src"
                       :compiler {:output-to "resources/public/js/cljs/main-debug.js"
                                  :optimizations :whitespace
                                  :pretty-print true}}
               :main {:source-path "src"
                      :compiler {:output-to "resources/public/js/cljs/main.js"
                                 :externs ["externs/jquery-1.8.js"
                                           "externs/externs.js"
                                           "externs/rx.js"]
                                 :optimizations :advanced
                                 :pretty-print false}}}})