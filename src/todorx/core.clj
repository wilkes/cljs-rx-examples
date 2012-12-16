(ns todorx.core
  (:use [noir.core :only [defpage]]
        [todorx.pages :only [layout]])
  (:require [noir.server :as server]))

(defpage "/" [] (layout))

(defn run-server [& [p]]
  (let [port (Integer. (or p 8080))]
    (server/start port {:mode :dev
                        :ns 'fermata})))

(comment
  (do
    (use 'todorx.core)
    (run-server)))