(ns comicdog.core
  (:require [net.cgrand.enlive-html :as e]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class :main true))



(defn extract-content-img [html]
  "extract image"
  (-> #"<img id = 'defualtPagePic' src=\"(.+?)\""
      (re-find  html)
      (second)))
;;mobile => #"id=\"comicimg\">.*?<img src=\"(.+?)\""
(def cc (get-html "http://comic.ck101.com/vols/11646139/195"))

(extract-content-filename cc)


(defn extract-content-next [html]
  "extract next page url"
  (-> #"<a href=\"(.*?)\" class=\"nextPageButton\""
      (re-find html)
      (second)))

(defn extract-content-filename [html]
  "extract current page number of this epsoid and return filename to store this image"
  (let [selector [:div.pageNumber :span.pageOne :> e/text-node]
        to-int #(. Integer parseInt %)
        format-name #(format "%03d.jpg" %)]
    (-> html 
        (extract-node selector)
        (to-int)
        (format-name))))

(defn extract-comic-name [html]
  "extract current comic name"
  (->> html
       (re-find #"<title>(.+?) - .*<\/title>")
       (second)
       (.trim)))

(def dd (get-html "http://comic.ck101.com/comic/6643/0"))



(def ee (extract-comic-lists dd))

(defn extract-comic-lists [html]
  "extract all epsoids of this comic"
  (extract-node html [:div.comicBox :div.relativeRec :h3.recTitle :a]
                ;;todo regex

                ))

;; (defn extract-epsoid-entry [html]
;;   "extract the entry url of this epsoid"
;;   )


(defn extract-epsoid-name [html]
  "extract the name of this epsoid for dir create"
  (-> html
      (extract-node [:h2.pageTitle :strong e/text-node])
      (.trim)))



(def ck101-extractor
  {:comic-name  extract-comic-name
   :comic-lists extract-comic-lists
   :epsoid-name extract-epsoid-name
   :content-filename extract-content-filename
   :content-image extract-content-img
   :content-next extract-content-next})
;;====================================================;;

(declare ^:dynamic *extractor*)


(defn mkdir
  ([name] (doto (java.io.File. name)
            (.mkdir)))
  ([name parent] (-> (java.io.File. parent  name)
                     (mkdir))))

(defn get-html [url]
  (let [response (client/get url)
        status (:status response)
        body   (:body response)]
    (if (or (= status 200) (= status 201))
      body
      (prn "download" url "fail"))))

(defn download-image [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out))
  (prn uri " ==> " (.getName file)))



(defn download-content [url dir]
  "download image by pages"
  (let [html (get-html url)
        filename ((:content-filename *extractor*) html)
        next ((:content-next *extractor*) html)
        imgurl ((:content-img *extractor*) html)
        file (java.io.File dir filename)]
    (when-not (nil? next)
      (if (.exist file)
        (prn "Ignore:" filename " exists!")
        (download-image imgurl file))
      (recur next dir))))



(defn download-episode [url dir]
  "Make dir and download episode"
  (let [html (get-html url)
        name ((:epsoid-name *extractor*) html)
        ;;entry ((:epsoid-entry *extractor*) html)
        epsoid-dir (mkdir name dir)]
    (download-content url epsoid-dir)))


(defn download-all [url]
  "Download all episodes of this comic"
  (let [html (get-html url)
        name ((:comic-name *extractor*) html)
        dir  (mkdir name)]
    (doseq [url ((:extract-comic-lists *extractor*) html)]
      (download-episode url dir))))

(defn go [url]
  (binding [*extractor* ck101-extractor]
    (download-all url)))







(defn extract-node [body selector]
  (let [snippet (e/html-snippet body)
        node (e/select snippet selector)]
    (apply str (e/emit* node))))


;; (defn get-books [url]
;;   (let [body (slurp url)
;;         node (extract-node body [:.comicBox :li :h3])
;;         regex #"href=\"(/vols/\d+/\d+)\".*?>(.*?)</a>"]
;;     (reverse (map (comp reverse rest) (re-seq regex node)))))

;; (def books  (get-books "http://comic.ck101.com/comic/5745" ))




;; (defn absolute-url [url]
;;   (cond
;;     (.startsWith url "http") url
;;     (.startsWith url "/") (str "http://comic.ck101.com" url)))



;; (defn visit-page[url]
;;   (let [aurl (absolute-url url)
;;         body (slurp aurl)
;;         imgurl (second (re-seq #"http.*?jpg" body))
;;         next-url (last (re-find #"href=\"(.*?)\"" (extract-node body [:a.nextPageButton])))]
;;     [imgurl, next-url]))

;; (defn seq-page [url]
;;   (let [[img next] (visit-page url)]
;;     (if (nil? next)
;;       [img]
;;       (cons img (lazy-seq (seq-page next))))))



;; (doseq [[name url] books]
;;   (let [aurl (absolute-url url)
;;         imgurls(seq-page aurl)
;;         dir (java.io.File. name)]
;;     (.mkdir dir)
;;     (prn "==>" name)
;;     (doseq [[i x] [imgurls range]]
;;       (spit "file.log" "test 1\n" :append true)
;;       )
;;     ))


;; (def cli-options
;;   ;; An option with a required argument
;;   [["-u" "--url Url" "Url"
;;     :u 80
;;     :parse-fn #(Integer/parseInt %)
;;     :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
;;    ["-h" "--help"]])

;; (defn -main [& args]
;;   (parse-opts args cli-options))
