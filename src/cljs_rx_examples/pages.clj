(ns cljs-rx-examples.pages
  (:require [hiccup.page :refer [html5
                            include-css
                            include-js]]
        [hiccup.element :refer [javascript-tag]]
        [hiccup.def :refer [defhtml]]))

(defhtml main-js [module js]
  (javascript-tag "var CLOSURE_NO_DEPS = true;")
  (include-js (str "/js/cljs/" js))
  (javascript-tag (str "cljs_rx_examples.client." module ".main();")))

(defn make-module [name label]
  {:label label
   :name name
   :dev (str "/ex/" name "/development")
   :prod (str "/ex/" name "/production")})

(def modules [(make-module "autocomplete" "Autocomplete")
              (make-module "dd" "Drag And Drop")
              (make-module "fly" "Time Flies")
              (make-module "paint" "Paint")
              {:label "todos"
               :name "todo"
               :dev "/index.html"
               :prod "/index.html"}])

(defhtml module-link [m]
  [:a {:href (:dev m)}  (:label m)])

(defhtml make-nav [module]
  [:ul.nav
   (for [m modules]
     [:li {:class (if (= (:name m) module)
                    "active")}
      (module-link m)])])

(defhtml mode-menu [mode module]
  (let [dev?  (= mode "development")]
    [:ul.nav.pull-right
     [:li {:class (if-not dev? "active")}
      [:a {:href (str "/ex/" module "/production")
           :class (if-not dev? "info")}
       "Production"]]
     [:li {:class (if dev? "active")}
      [:a {:href (str "/ex/" module "/development")
           :class (if dev? "info")}
       "Development"]]]))

(defhtml layout [mode module]
  (html5
   [:head
    [:title (str "RxJS Examples: " module)]
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=7;IE=8;IE=edge"}]
    "<!--[if lt IE 9]>
        <script src=\"http://html5shiv.googlecode.com/svn/trunk/html5.js\"></script>
     <![endif]-->"
    (javascript-tag "if (typeof console === \"undefined\") {
        console = {}; // define it if it doesn't exist already
        console.log = function() {};
        console.dir = function() {};}")
    (javascript-tag "if (typeof window.BlobBuilder === \"undefined\") {
        window.BlobBuilder = function() {}; // define it if it doesn't exist already
        }")
    (include-css "/css/bootstrap.css"
                 "/css/font-awesome.css")]
   [:body {:style "padding-top: 60px;"}
    [:div.navbar.navbar-fixed-top
     [:div.navbar-inner
      [:div.container
       (make-nav module)
       (mode-menu mode module)]]]
    [:div#main-content.container]
    (include-js "/js/jquery-1.8.1.min.js"
                "/js/bootstrap.min.js"
                "/js/rx.js"
                "/js/rx.time.js"
                "/js/rx.binding.js"
                "/js/rx.jquery.js")
    (main-js module
             (if (= "development" mode)
               "main-debug.js"
               "main.js"))]))
