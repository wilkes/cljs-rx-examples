(ns cljs-rx.observable
  (:require [jayq.core :refer [$] :as j]))

(defn select [obs f]
  (.select obs f))

(defn select-val [obs]
  (select obs
          (fn [e]
            (j/val ($ (.-target e))))))

(defn throttle [obs ms]
  (.throttle obs ms))

(defn distinct-until-changed [obs]
  (.distinctUntilChanged obs))

(defn where [obs f]
  (.where obs f))

(defn switch-latest [obs]
  (.switchLatest obs))

(defn subscribe
  ([obs observer-or-on-next]
     (.subscribe obs observer-or-on-next))
  ([obs observer-or-on-next, on-error]
     (.subscribe obs observer-or-on-next on-error))
  ([obs observer-or-on-next on-error on-completed]
     (.subscribe obs observer-or-on-next on-error on-completed)))