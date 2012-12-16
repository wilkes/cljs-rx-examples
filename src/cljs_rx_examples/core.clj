(ns cljs-rx-examples.core
  (:use [noir.core :only [defpage]]
        [cljs-rx-examples.pages :only [layout]])
  (:require [noir.server :as server]))

(defpage "/ex/:module/:mode" {:keys [mode module]}
  (layout mode module))

(defn run-server [& [p]]
  (let [port (Integer. (or p 8080))]
    (server/start port {:mode :dev
                        :ns 'cljs-rx-examples})))

(comment
  (do
    (use 'cljs-rx-examples.core)
    (run-server)))