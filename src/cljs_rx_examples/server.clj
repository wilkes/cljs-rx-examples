(ns cljs-rx-examples.server
  (:require [noir.server :as server]
            [noir.core :refer [defpage]]
            [noir.response :as response]
            [cljs-rx-examples.pages :refer [layout]]))

(defpage "/" []
  (response/redirect "/ex/autocomplete/development"))

(defpage "/ex/:module/:mode" {:keys [mode module]}
  (layout mode module))

(defn run-server [& [p]]
  (let [port (Integer. (or p 8080))]
    (server/start port {:mode :dev
                        :ns 'cljs-rx-examples})))

(comment
  (do
    (use 'cljs-rx-examples.server)
    (run-server)))