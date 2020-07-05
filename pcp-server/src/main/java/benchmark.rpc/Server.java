package benchmark.rpc;

import io.github.free.lock.pcp.PcpClient;
import io.github.free.lock.pcprpc.CommandData;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import io.github.free.lock.pcp.BoxFun;
import io.github.free.lock.pcp.PcpServer;
import io.github.free.lock.pcp.Sandbox;
import scala.collection.immutable.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.*;
import static scala.compat.java8.JFunction.*;


public class Server {

    public static final String host = "benchmark-server";
    public static final int port = 8080;

    @SuppressWarnings("unchecked")
    private static <K, V> scala.collection.immutable.Map<K, V> toScalaImmutableMap(java.util.Map<K, V> javaMap) {
        final java.util.List<scala.Tuple2<K, V>> list = new java.util.ArrayList<>(javaMap.size());
        for (final java.util.Map.Entry<K, V> entry : javaMap.entrySet()) {
            list.add(scala.Tuple2.apply(entry.getKey(), entry.getValue()));
        }
        final scala.collection.Seq<Tuple2<K, V>> seq = scala.collection.JavaConverters.asScalaBufferConverter(list).asScala().toSeq();
        return (scala.collection.immutable.Map<K, V>) scala.collection.immutable.Map$.MODULE$.apply(seq);
    }

    public static void main(String[] args) throws Exception {
        Map<String, BoxFun> funMap = new HashMap<String, BoxFun>();
        scala.Function2 func1 = func((List<Object> list, PcpServer pcpServer) -> {
            return 1;
        });
        funMap.put("add", Sandbox.toSanboxFun(func1));
        Sandbox sandbox = new Sandbox(toScalaImmutableMap(funMap));

        PcpServer pcpServer = new PcpServer(sandbox);

        pcpServer.execute("[\"add\", 1, 2]");

        PcpClient p = new PcpClient();
        // """["add", 1, ["add", 2, 3]]"""
        // output: 6
        LinkedList list = new LinkedList();
        list.add(2);
        list.add(3);
        System.out.println(pcpServer.execute(
                p.toJson(
                        p.call("add", JavaConverters.collectionAsScalaIterable(list).toSeq())
                )
        ));
    }
}