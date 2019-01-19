docker build -t  alexandru.lungu/kubernetes-java-annotations:0.0.1-SNAPSHOT .
kubectl apply -f ./build/classes/java/main/META-INF/ap4k/kubernetes.yml