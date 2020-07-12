package benchmark.rpc;

import benchmark.bean.Page;
import benchmark.service.UserService;
import com.google.gson.Gson;
import io.github.free.lock.pcprpc.PcpRpc;
import io.github.free.lock.saio.AIO;
import io.github.free.lock.saio.AIOServer;
import io.github.free.lock.sjson.PathNode;
import scala.Tuple2;
import io.github.free.lock.pcp.BoxFun;
import io.github.free.lock.pcp.PcpServer;
import io.github.free.lock.pcp.Sandbox;
import io.github.free.lock.sjson.JSON;
import io.github.free.lock.sjson.JSONParser;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import benchmark.bean.User;
import benchmark.service.UserServiceServerImpl;
import static scala.compat.java8.JFunction.*;
import scala.collection.mutable.Stack;
import scala.reflect.ClassTag$;


public class Server {

    public static final String host = "0.0.0.0";
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
            String email = list.head().toString();
            return userService.existUser(email);
        });

        scala.Function2 createUser = func((List<Object> list, PcpServer pcpServer) -> {
            Gson g = new Gson();
            User user = g.fromJson(list.head().toString(), User.class);
            return userService.createUser(user);
        });

        scala.Function2 getUser = func((List<Object> list, PcpServer pcpServer) -> {
            Object head = list.head();
            User user = userService.getUser(Long.parseLong(head.toString()));
            return user;
        });

        scala.Function2 listUser = func((List<Object> list, PcpServer pcpServer) -> {
            Page<User> result = userService.listUser(Integer.parseInt(list.head().toString()));
            System.out.println("listUser: " + result);
            return result;
        });
        funMap.put("existUser", Sandbox.toSanboxFun(existUser));
        funMap.put("createUser", Sandbox.toSanboxFun(createUser));
        funMap.put("getUser", Sandbox.toSanboxFun(getUser));
        funMap.put("listUser", Sandbox.toSanboxFun(listUser));
        return new Sandbox(toScalaImmutableMap(funMap));
    }

    public static void main(String[] args) {
        try {
            AIOServer.Server server = PcpRpc.getPCServer(host, port, getSandbox(), ExecutionContext.global());
            System.out.println(server.server() == server.server());
            System.out.println(server.server().getLocalAddress());
            System.out.println(server.server().isOpen());
            System.out.println(server.server().getLocalAddress());
            System.out.println("exiting?");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}