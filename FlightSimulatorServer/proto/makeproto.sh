#!/bin/bash

#kendi kullanıcı adınız ile değişecek. Kullanmak üzere classlar oluşturulur.
$HOME/.nuget/packages/grpc.tools/1.15.0/tools/linux_x64/protoc -I . --csharp_out . --grpc_out . *.proto --plugin=protoc-gen-grpc=$HOME/.nuget/packages/grpc.tools/1.15.0/tools/linux_x64/grpc_csharp_plugin
