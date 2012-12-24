(defproject cljs-rx-examples "0.1.0-SNAPSHOT"
  :description "Messing around with RxJS and ClojureScript"
  :url "https://github.com/wilkes/cljs-rx-examples"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.2"]
                 [jayq "1.0.0"]
                 [crate "0.2.3"]]
  :plugins [[lein-cljsbuild "0.2.9"]
            [lein-ring "0.7.5"]]
  :ring {:handler cljs-rx-examples.server/app}
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