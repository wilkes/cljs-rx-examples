(ns autocomplete.core
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def log-pr (comp log pr-str))

(def $content ($ "#main-content"))

(defn throttled-input []
  (-> ($ "#search-input")
      (rxj/keyup)
      (rx/select #(j/val ($ "#search-input")))
      (rx/throttle 500)
      (rx/where (fn [v]
                  (> (count v) 2)))
      rx/distinct-until-changed))

(defn search-wikipedia [term]
  (rxj/ajax "http://en.wikipedia.org/w/api.php"
            {:data {:action "opensearch"
                    :search term
                    :format :json}
             :dataType :jsonp}))

(defn suggestions [input]
  (-> input
      (rx/select search-wikipedia)
      rx/switch-latest
      (rx/where (fn [{:keys [data]}]
                  (and (:0 data) (:1 data))))))

(defn subscribe-suggestions [sug]
  (let [$elem ($ "#results")]
    (rx/subscribe sug
                  (fn [{:keys [data]}]
                    (j/empty $elem)
                    (doseq [t (:1 data)]
                      (j/append $elem
                                (str "<li>" t "</li>"))))
                  (fn [e]
                    (j/inner $elem
                             (str "<li>" e "</li>")))
                  (fn []
                    (j/append $elem
                              "<li>Search Completed</li>")))))

(defpartial content []
  [:div.row
   [:h2 "Search Wikipedia"]
   [:input#search-input.input-xlarge]
   [:ul#results.unstyled]])

(defn initialize []
  (j/append $content (content))
  (let [input (throttled-input)
        wiki (suggestions input)]
    (subscribe-suggestions wiki)
    (rx/subscribe input log-pr)
    (rx/subscribe wiki log-pr))
  (.focus ($ :#search-input)))

(defn ^:export main []
  (initialize))