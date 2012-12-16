(ns snorty.client.core
  (:require [jayq.core :refer [$] :as j]
            [clojure.browser.repl :as repl]))

(def $content ($ "#main-content"))
(def listeners (atom {}))

(defn search-obs []
  (-> ($ "#search-input")
      (.keyupAsObservable)
      (.select (fn [_] (j/val ($ "#search-input"))))
      (.throttle 500)
      (.distinctUntilChanged)))

(defn connect [atm & connections]
  (doseq [[observer & hookups] connections]
    (let [ob (observer)]
      (doseq [hook hookups]
        (let [key (keyword (.-name observer))
              subscription (.subscribe ob hook)]
          (swap! atm assoc key (conj (get @atm key) subscription)))))))

(defn disconnect [atm]
  (doseq [[n subs] @atm]
    (doseq [sub subs]
      (.dispose sub))))

(defn initialize []
  (j/append $content "<h2>RxJS Examples</h2>")
  (j/append $content "<label for=\"search-input\">Search</label> <input id=\"search-input\" />")
  (j/append $content "<div id=\"results\"/>")
  (connect listeners
           [search-obs
            (fn [term]
              (j/append ($ "#results") (format "%s <br/>" term)))]))

(defn ^:export main []
  (initialize))

(defn ^:export repl []
  (repl/connect "http://localhost:9000/repl"))