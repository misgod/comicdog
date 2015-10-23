(ns comicdog.core
  (:require [net.cgrand.enlive-html :as e]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clj-http.conn-mgr :refer [make-reusable-conn-manager shutdown-manager]]
            [comicdog.common :refer :all]
            [comicdog.sites.ck101 :refer :all]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class :main true))


(declare ^:dynamic *extractor*)


(defn mkdir
  ([name] (doto (io/file name)
            (.mkdir)))
  ([name parent] (-> (io/file parent  name)
                     (mkdir))))

(defn do-erro-log [dir]
  (spit "error.log" (str (.getName dir) "\n") :append true))

(def cm (make-reusable-conn-manager {:timeout 10 :threads 2}))


(defn get-html [url]
  (let [response (client/get url {:connection-manager cm})
        status (:status response)
        body   (:body response)]
    (if (or (= status 200) (= status 201))
      body
      (prn "download" url "fail"))))

(defn mark-episode-done [dir]
  "Put a .done file in the dir to avoid rescan it"
  (.createNewFile (io/file dir ".done")))



(defn is-episode-done [dir]
  "Check if there is .done in this dir"
  (.exists (io/file dir ".done")))


(defn download-image [uri file]
  (try
    (io/copy
     (:body (client/get uri {:connection-manager cm :as :stream}))
     (io/file file))
    (prn "Done!\n")
    (catch Exception e (prn  "error!\n " (.getMessage e)))))

(defn download-content [url dir]
  "download image by pages"
  (let [html (get-html url)
        filename ((:content-filename *extractor*) html)
        next ((:content-next *extractor*) html)
        imgurl ((:content-image *extractor*) html)
        file (io/file dir filename)]
    (print "download " filename " => ")

    (if (.exists file)
      (print "exists!\n")
      (download-image imgurl file))

    (when-not (nil? next)
      (recur next dir))))



(defn download-episode [url dir]
  "Make dir and download episode"
  (let [html (get-html url)
        name ((:episode-name *extractor*) html)
        ;;entry ((:episode-entry *extractor*) html)
        episode-dir (mkdir name dir)]
    (prn "Download episode:" name)
    (when-not (is-episode-done episode-dir)
      (try
        (download-content url episode-dir)
        (mark-episode-done episode-dir)
        (catch Exception e (do
                             (do-erro-log episode-dir)
                             (prn "somethin error!!!")
                             (prn (.getMessage e))))))))




(defn download-all [url]
  "Download all episodes of this comic"
  (let [html (get-html url)
        name ((:comic-name *extractor*) html)
        dir  (mkdir name)]
    (doseq [episode-url ((:episode-lists *extractor*) html)]
        (download-episode episode-url dir))))

(defn go [url]
  (binding [*extractor* (extractor url)]
    (download-all url)))


(defn -main [& args]
  (if (empty? args)
    (prn "no url")
    (doseq [url args] (go url)))
  (shutdown-manager cm))
