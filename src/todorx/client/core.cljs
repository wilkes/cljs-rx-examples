(ns todorx.client.core
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def log-pr (comp log pr-str))

(def $content ($ "#main-content"))

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
  [:div
   [:h2 "RxJS Examples"]
   [:label {:for "search-input"} "Search"]
   [:input#search-input]
   [:ul#results]])

(defn initialize []
  (j/append $content (content))
  (-> ($ "#search-input")
      (rxj/throttled-input 500)
      (rx/where #(> (count %) 2))
      suggestions
      subscribe-suggestions))

(defn ^:export main []
  (initialize))