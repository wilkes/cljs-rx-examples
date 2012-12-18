(ns cljs-rx.clojure
  (:require [clojure.data :as data]))

#_(defn observable-atom [atm]
  (let [k (gensym)]
    (js/Rx.Observable.create
     (fn [observer]
       (add-watch atm k
                  (fn [_ _ _ new-state]
                    (.onNext observer new-state)))
       (fn [] (remove-watch atm k))))))

(defn observable-atom [atm]
  (let [k (gensym)
        subject (js/Rx.BehaviorSubject.)]
    (add-watch atm k
               (fn [_ _ _ new-state]
                 (.onNext subject new-state)))
    subject))

(declare clone-obs-map)

(defprotocol IObservable
  (subscribe
    [obs observer-or-on-next]
    [obs observer-or-on-next, on-error]
    [obs observer-or-on-next on-error on-completed]))

(deftype ObservableMap [meta content subject]
  Object
  (toString [this]
    (pr-str {:content content
             :subject subject}))

  IWithMeta
  (-with-meta [coll meta]
    (clone-obs-map meta content subject))

  IMeta
  (-meta [coll] meta)

  ICollection
  (-conj [coll entry]
    (let [new-value (conj content entry)]
      (.onNext subject new-value)
      (clone-obs-map meta new-value subject)))

  IEmptyableCollection
  (-empty [coll]
    (.onNext subject cljs.core.PersistentArrayMap/EMPTY)
    (clone-obs-map meta
                   cljs.core.PersistentArrayMap/EMPTY
                   subject))

  IEquiv
  (-equiv [coll other] (equiv-map content (.-content other)))

  IHash
  (-hash [coll] (hash content))

  ISeqable
  (-seq [coll] (seq content))

  ICounted
  (-count [coll] (count content))

  ILookup
  (-lookup [coll k]
    (-lookup coll k nil))

  (-lookup [coll k not-found]
    (get content k not-found))

  IAssociative
  (-assoc [coll k v]
    (let [new-value (assoc content k v)]
      (.onNext subject new-value)
      (clone-obs-map meta new-value subject)))

  (-contains-key? [coll k]
    (contains? content k))

  IMap
  (-dissoc [coll k]
    (let [new-value (dissoc content k)]
      (.onNext subject new-value)
      (clone-obs-map meta new-value subject)))

  IKVReduce
  (-kv-reduce [coll f init]
    (reduce content f init))

  IFn
  (-invoke [coll k]
    (-lookup coll k))

  (-invoke [coll k not-found]
    (-lookup coll k not-found))

  IObservable
  (subscribe [obs observer-or-on-next]
    (.subscribe subject observer-or-on-next))

  (subscribe [obs observer-or-on-next, on-error]
    (.subscribe subject observer-or-on-next on-error))

  (subscribe [obs observer-or-on-next on-error on-completed]
    (.subscribe subject observer-or-on-next on-error on-completed)))

(defn- clone-obs-map [meta content subject]
  (ObservableMap. meta content subject))

(defn observable-map [m]
  (ObservableMap. nil m (js/Rx.BehaviorSubject.)))
