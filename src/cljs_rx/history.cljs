(ns cljs-rx.history
  (:require [goog.events :as gevents]
            [goog.History :as ghistory]
            [goog.history.EventType :as history-event]
            [goog.history.Html5History :as history5]))

(declare history)

(defn get-token [] (.getToken history))
(defn set-token! [token] (.setToken history token))
(defn replace-token! [token] (.replaceToken history token))

(defn init-history []
  (let [h (if (history5/isSupported)
            (goog.history.Html5History.)
            (goog.History.))]
    (.setEnabled h true)
    h))

(def history (init-history))

(def history-observable
  (js/Rx.Observable.create
   (fn [observer]
     (let [listener (gevents/listen history history-event/NAVIGATE
                                    (fn [e]
                                      (.onNext observer
                                               {:token (keyword (.-token e))
                                                :type (.-type e)
                                                :navigation? (.-isNavigation e)})))])
     (fn []
       (gevents/unlisten history history-event/NAVIGATE listener)))))