(ns cljs-rx.clojure
  (:require [cljs-rx.observable :as rx]
            [cljs-rx.subject :as subject]
            [clojure.data :as data]
            [jayq.util :refer [log]]))

(defn observable-atom [atm]
  (let [k (gensym)
        subject (subject/behavior)]
    (add-watch atm k
               (fn [_ _ _ new-state]
                 (.onNext subject new-state)))
    subject))

(defprotocol IObservable
  (subscribe
    [obs observer-or-on-next]
    [obs observer-or-on-next, on-error]
    [obs observer-or-on-next on-error on-completed]))

(defprotocol IObservableWrapper
  (as-obs [this])
  (-set-content [this type new-content]))

(defn- subscribe-parent [parent child]
  (if (satisfies? IObservableWrapper child)
    (rx/subscribe (as-obs child)
                  (fn [x]
                    (.onNext (as-obs parent) (.-content parent)))))
  parent)

(deftype ObservableMap [meta content subject]
  IObservableWrapper

  (as-obs [this] subject)

  (-set-content [this type new-content]
    (set! (.-content this) new-content)
    (.onNext subject content)
    this)

  IPrintWithWriter
  (-pr-writer [this writer opts]
    (-write writer "#<ObservableMap: ")
    (-pr-writer content writer opts)
    (-write writer ">"))

  Object
  (toString [_]
    (pr-str {:content content
             :subject subject}))

  IWithMeta
  (-with-meta [this meta]
    (set! (.-meta this) meta)
    this)

  IMeta
  (-meta [_] meta)

  ICollection
  (-conj [this entry]
    (-set-content this :add (-conj content entry))
    (subscribe-parent this entry))

  IEmptyableCollection
  (-empty [this]
    (-set-content this :remove cljs.core.PersistentArrayMap/EMPTY))

  IEquiv
  (-equiv [_ other] (equiv-map content (.-content other)))

  IHash
  (-hash [this] (goog.getUid this))

  ISeqable
  (-seq [_] (-seq content))

  ICounted
  (-count [_] (-count content))

  ILookup
  (-lookup [coll k]
    (-lookup coll k nil))

  (-lookup [_ k not-found]
    (-lookup content k not-found))

  IAssociative
  (-assoc [this k v]
    (-set-content this :add (-assoc content k v))
    (subscribe-parent this v)) ;; not sure about this

  (-contains-key? [_ k]
    (-contains-key? content k))

  IMap
  (-dissoc [this k]
    (-set-content this :remove (-dissoc content k)))

  IKVReduce
  (-kv-reduce [_ f init]
    (-kv-reduce content f init))

  IFn
  (-invoke [coll k]
    (-lookup coll k))

  (-invoke [coll k not-found]
    (-lookup coll k not-found))

  IObservable
  (subscribe [_ observer-or-on-next]
    (.subscribe subject observer-or-on-next))

  (subscribe [_ observer-or-on-next, on-error]
    (.subscribe subject observer-or-on-next on-error))

  (subscribe [_ observer-or-on-next on-error on-completed]
    (.subscribe subject observer-or-on-next on-error on-completed)))

(defn observable-map [m]
  (ObservableMap. nil m (subject/behavior)))

(deftype ObservableVector [meta content subject]
  IObservableWrapper
  (as-obs [this] subject)

  (-set-content [this type new-content]
    (set! (.-content this) new-content)
    (.onNext subject content)
    this)

  IPrintWithWriter
  (-pr-writer [this writer opts]
    (-write writer "#<ObservableVector: ")
    (-pr-writer content writer opts)
    (-write writer ">"))

  Object
  (toString [_]
    (pr-str {:type ObservableVector
             :content content
             :subject subject}))

  IWithMeta
  (-with-meta [this meta]
    (set! (.-meta this) meta)
    this)

  IMeta
  (-meta [_] meta)

  IStack
  (-peek [coll]
    (-peek content))
  (-pop [this]
    (-set-content this :remove (-pop content)))

  ICollection
  (-conj [this entry]
    (-set-content this :add (-conj content entry))
    (subscribe-parent this entry))

  IEmptyableCollection
  (-empty [this]
    (-set-content this :remove cljs.core.PersistentVector/EMPTY))

  ISequential
  IEquiv
  (-equiv [_ other] (equiv-sequential content (.-content other)))

  IHash
  (-hash [this] (goog.getUid this))

  ISeqable
  (-seq [_]
    (-seq content))

  ICounted
  (-count [_] (-count content))

  IIndexed
  (-nth [_ n]
    (-nth content n))
  (-nth [_ n not-found]
    (-nth content n not-found))

  ILookup
  (-lookup [coll k] (-nth coll k nil))
  (-lookup [coll k not-found] (-nth coll k not-found))

  IMapEntry
  (-key [coll]
    (-nth coll 0))
  (-val [coll]
    (-nth coll 1))

  IAssociative
  (-assoc [this k v]
    (-set-content this :add (-assoc content k v))
    (subscribe-parent this v))

  IVector
  (-assoc-n [coll n val] (-assoc coll n val))

  IReduce
  (-reduce [_ f]
    (-reduce content f))
  (-reduce [_ f start]
    (-reduce content f start))

  IKVReduce
  (-kv-reduce [_ f init]
    (-kv-reduce content f init))

  IFn
  (-invoke [coll k]
    (-lookup coll k))
  (-invoke [coll k not-found]
    (-lookup coll k not-found))

  IReversible
  (-rseq [coll]
    (-rseq content))

  IObservable
  (subscribe [_ observer-or-on-next]
    (.subscribe subject observer-or-on-next))

  (subscribe [_ observer-or-on-next, on-error]
    (.subscribe subject observer-or-on-next on-error))

  (subscribe [_ observer-or-on-next on-error on-completed]
    (.subscribe subject observer-or-on-next on-error on-completed)))

(defn observable-vector [v]
  (ObservableVector. nil v (subject/behavior)))