(ns cljs-rx-examples.client.autocomplete
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn throttled-input []
  (-> (rxj/keyup ($ "#search-input"))
      (rx/select #(j/val ($ "#search-input")))
      (rx/throttle 500)
      (rx/where #(> (count %) 2))
      rx/distinct-until-changed))

(defn search-wikipedia [term]
  (rxj/ajax "http://en.wikipedia.org/w/api.php"
            {:data {:action "opensearch"
                    :search term
                    :format :json}
             :dataType :jsonp}))

(defn suggestions []
  (-> (throttled-input)
      (rx/select search-wikipedia)
      rx/switch-latest
      (rx/where (fn [{:keys [data]}]
                  (and (:0 data) (:1 data))))))

(defpartial li [t] [:li t])

(defn display-response  [$elem]
  (fn [{:keys [data]}]
    (j/empty $elem)
    (doseq [t (:1 data)]
      (j/append $elem (li t)))))

(defn display-error [$elem]
  (fn [e]
    (j/inner $elem (li e))))

(defpartial content []
  [:div.row
   [:h2 "Search Wikipedia"]
   [:input#search-input.input-xlarge]
   [:ul#results.unstyled]])

(defn ^:export main []
  (j/append ($ "#main-content")
            (content))
  (let [$elem ($ "#results")]
    (rx/subscribe (suggestions)
                  (display-response $elem)
                  (display-error $elem)))
  (.focus ($ :#search-input)))