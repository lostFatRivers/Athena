#coding: utf8
import os
import os.path

root_dir = "./protocol/"

proto_dir = "./protocol"
java_out_dir = "../java"

for dirpath, sub_dirs, filenames in os.walk(root_dir):
    print(filenames)
    for eachName in filenames:
        protoc_command = "protoc -I=" + proto_dir + " --java_out=" + java_out_dir + " " + proto_dir + "/" + eachName
        print(protoc_command)
        result = os.system(protoc_command)
        print(result)
#os.popen("protoc -I=" proto_dir "--java_out=" java_out_dir "--js_out=" js_out_dir)
#protoc -I=$SRC_DIR --java_out=$DST_DIR $SRC_DIR/addressbook.proto