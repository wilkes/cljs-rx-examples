(ns cljs-rx.observable)

(defn select [obs f]
  (.select obs f))

(defn select-many [obs f]
  (.selectMany obs f))

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

(defn delay [obs ms]
  (.delay obs ms))

(defn take-until [obs obs2]
  (.takeUntil obs obs2))