(ns todorx.pages
  (:use [hiccup.page :only [html5
                            include-css
                            include-js]]
        [hiccup.element :only [javascript-tag]]
        [noir.core :only [defpartial]]
        [noir.options :only [dev-mode?]])
  (:require [noir.session :as session]
            [noir.validation :as vali]))

(defpartial dev-js []
  (javascript-tag "var CLOSURE_NO_DEPS = true;")
  (include-js "js/cljs/main-debug.js")
  (javascript-tag "todorx.client.core.main();"))

(defpartial prod-js []
  (javascript-tag "var CLOSURE_NO_DEPS = true;")
  (include-js "js/cljs/main.js")
  (javascript-tag "todorx.client.core.main();"))

(defpartial layout [& [content]]
  (html5
   [:head
    [:title "RxJS Testing"]
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
                 "/css/font-awesome.css"
                 "/css/bootstrap-responsive.css")]
   [:body
    [:div.wrapper
     [:div#shell.container-fluid
      [:div.row-fluid
       [:div#main-content content]]]]
    (include-js "/js/jquery-1.8.1.min.js"
                "/js/bootstrap.min.js"
                "/js/rx.js"
                "/js/rx.time.js"
                "/js/rx.jquery.js")
    (if (dev-mode?)
      (dev-js)
      (prod-js))]))
