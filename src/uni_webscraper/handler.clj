; Author: Chris Nicholas
; Date 11/4/17
; This is the main entry point for the program
; It provides a number of functions including:
;   * routes for the server to hook into
;   * logs requests to console
;   * wraps json responses


(ns uni-webscraper.handler
  (:use compojure.core
        ring.middleware.json)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [org.httpkit.client :as http]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.json :refer [wrap-json-response]]
            [uni-webscraper.util_funcs :as utils]
            [uni-webscraper.webscrape :as scrape])
  (:require [clojure.string :as str]))


; Take params as a query string and break down into component parts
(defn handle-params [params]
  (println (utils/split-params params))) 


; Define access routes from webpage via server
(defroutes app-routes
  ; Root request responds with index.html
  (GET "/" [] (resource-response "index.html" {:root "public"}))

  ; Post request consumes parameters ready for processing
  ; Retrieves parameters from server and invokes processing of
  ; parameters and results. This is the main starting point
  ; for all processing
  (POST "/parameters" {body :body} 
        (let [data (slurp body)] 
          (println data)
          (scrape/webscrape data)
          (response {:message "OK"})))
  
  ; Response for a requested route that doesn't exist
  (route/not-found 
   (response {:message "Page not found"}))) 


; Middleware, This logs requests from the server to the console
; and passes the request onto the handler function
(defn wrap-log-request [handler]
  (fn [req] ; return handler function
    (println (prn-str "My Request:" req)) ; perform logging
    (handler req))) ; pass the request through to the inner handler

; Wraps requests into json
(def app
  (-> app-routes
      wrap-log-request
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-json-params)
  )



