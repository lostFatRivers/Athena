package com.jokerbee.report

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class GameReporter {
    def rootPath = "127.0.0.1:8030"
    def http = new HTTPBuilder()


    void baseHttp(String path, Method method, String content) {
        http.request(method, ContentType.JSON) { req ->
            uri.path = rootPath + path
            body = content

        }
    }
}
