(ns cljs-rx.connect)

(defn make-connector []
  (atom {}))

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