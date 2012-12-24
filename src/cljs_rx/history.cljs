(ns cljs-rx.history
  (:require [cljs-rx.observable :as rx]
   [goog.events :as gevents]
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
    ;; lifted from https://github.com/shoreleave/shoreleave-browser/blob/master/src/shoreleave/browser/history.cljs
    (gevents/unlisten (.-window_ h)
                      (.-POPSTATE gevents/EventType) ; This is a patch-hack to ignore double events
                      (.-onHistoryEvent_ h)
                      false
                      h)
    h))

(def history (init-history))

(defn init-history-observable []
  (js/Rx.Observable.create
   (fn [observer]
     (let [handler (fn [e]
                     (.onNext observer
                              {:token (.-token e)
                               :type (.-type e)
                               :navigation? (.-isNavigation e)}))
           listener (gevents/listen history
                                    history-event/NAVIGATE
                                    handler)]
       (fn []
         (gevents/unlisten history history-event/NAVIGATE listener)))))
  )

(def history-observable
  (rx/distinct-until-changed (init-history-observable)))