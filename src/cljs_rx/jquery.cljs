(ns cljs-rx.jquery
  (:require  [cljs-rx.observable :as rx]
             [jayq.core :refer [$] :as j]
             [jayq.util :refer [clj->js log]]))

(def log-pr (comp log pr-str))
(defn keyup [$elem]
  (.keyupAsObservable $elem))

(defn ajax
  ([url settings]
     (ajax (assoc settings :url url)))
  ([settings]
     (let [settings (j/->ajax-settings settings)]
       (-> (.ajaxAsObservable js/jQuery settings)
           (rx/select (fn [val] (js->clj val :keywordize-keys true)))))))

(defn throttled-input [$input speed]
  (-> $input
      keyup
      rx/select-val
      (rx/throttle speed)
      rx/distinct-until-changed))