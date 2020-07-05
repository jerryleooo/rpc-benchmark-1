package benchmark.rpc;

import benchmark.service.UserService;
import io.github.free.lock.pcprpc.PcpRpc;
import scala.Tuple2;
import io.github.free.lock.pcp.BoxFun;
import io.github.free.lock.pcp.PcpServer;
import io.github.free.lock.pcp.Sandbox;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import benchmark.bean.User;
import benchmark.service.UserServiceServerImpl;
import static scala.compat.java8.JFunction.*;

public class Server {

    public static final String host = "127.0.0.1";
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

    private static Sandbox getSandbox() {
        UserService userService = new UserServiceServerImpl();

        Map<String, BoxFun> funMap = new HashMap<String, BoxFun>();
        scala.Function2 existUser = func((List<Object> list, PcpServer pcpServer) -> {
            return userService.existUser((String)list.head());
        });

        scala.Function2 createUser = func((List<Object> list, PcpServer pcpServer) -> {
            return userService.createUser((User)list.head());
        });

        scala.Function2 getUser = func((List<Object> list, PcpServer pcpServer) -> {
            return userService.getUser((Long)list.head());
        });

        scala.Function2 listUser = func((List<Object> list, PcpServer pcpServer) -> {
            return userService.listUser((Integer)list.head());
        });
        funMap.put("existUser", Sandbox.toSanboxFun(existUser));
        funMap.put("createUser", Sandbox.toSanboxFun(createUser));
        funMap.put("getUser", Sandbox.toSanboxFun(getUser));
        funMap.put("listUser", Sandbox.toSanboxFun(listUser));
        return new Sandbox(toScalaImmutableMap(funMap));
    }

    public static void main(String[] args) throws Exception {

        PcpRpc.getPCServer(host, port, getSandbox(), ExecutionContext.global());

        /*
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
        */
    }
}