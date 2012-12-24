(ns cljs-rx-examples.server
  (:require [cljs-rx-examples.pages :as pages]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]))

(defroutes app-routes
  (GET "/ex/:module/:mode" [module mode] (pages/layout mode module))
  (GET "/" [] (response/redirect "/ex/autocomplete/development"))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (handler/site app-routes))