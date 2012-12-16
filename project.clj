(defproject snorty "0.1.0-SNAPSHOT"
  :description "Messing around with RxJS and ClojureScript"
  :url "https://github.com/sethtrain/snorty"
  :dependencies [[clj-http "0.5.7"]
                 [speclj "2.3.1"]
                 [noir "1.3.0-beta10"]
                 [jayq "0.3.0"]
                 [fetch "0.1.0-alpha2"]
                 [crate "0.2.1"]
                 [korma "0.3.0-beta11"]
                 [postgresql "9.1-901.jdbc4"]
                 [org.clojure/clojure "1.4.0"]
                 [com.draines/postal "1.8.0"]
                 [clj-time "0.4.4"]
                 [lib-noir "0.2.0-alpha2"]]
  :plugins [[speclj "2.3.1"]
            [lein-cljsbuild "0.2.9"]]
  :test-paths ["spec/"]
  :cljsbuild {:builds
              {:debug {:source-path "src/snorty/client"
                       :compiler {:output-to "resources/public/js/cljs/main-debug.js"
                                  :optimizations :whitespace
                                  :pretty-print true}}
               :main {:source-path "src/snorty/client"
                      :compiler {:output-to "resources/public/js/cljs/main.js"
                                 :externs ["externs/jquery-1.8.js"
                                           "externs/externs.js"]
                                 :optimizations :advanced
                                 :pretty-print false}}}})