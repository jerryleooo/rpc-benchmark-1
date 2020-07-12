package benchmark.rpc;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.free.lock.pcp.PcpClient;
import io.github.free.lock.pcprpc.PcpRpc;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import scala.concurrent.duration.Duration;

import io.github.free.lock.sjson.JSON;
import scala.collection.JavaConverters;
import scala.concurrent.Await;
import scala.compat.java8.FutureConverters;
import scala.collection.mutable.Stack;



public class UserServicePCPClientImpl implements UserService, Closeable {

    private final String host = "benchmark-server";
    private final int port = 8080;
    private final PcpRpc.ClientPool client;
    private final PcpClient p = new PcpClient();


    public UserServicePCPClientImpl(PcpRpc.ClientPool client) {
        this.client = client;
    }

    @Override
    public boolean existUser(String email) {
        try {
            System.out.println("existUser");
            scala.concurrent.Future future = client.call(
                    p.call("existUser", JavaConverters.asScalaBuffer(List.of(email))),
                    2 * 60 * 1000,
                    10 * 1000
            );
            String resultString = Await.result(future, Duration.Inf()).toString();
            System.out.println("existUser: " + resultString);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean createUser(User user) {
        try {
            System.out.println("createUser");
            scala.concurrent.Future future = client.call(
                    p.call("createUser", JavaConverters.asScalaBuffer(List.of(JSON.stringify(user, (Object data, Stack<String> path) -> null)))),
                    2 * 60 * 1000,
                    10 * 1000
            );
            String resultString = Await.result(future, Duration.Inf()).toString();
            System.out.println("createUser: " + resultString);
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public User getUser(long id) {
        try {
            System.out.println("getUser");

            scala.concurrent.Future future = client.call(
                    p.call("getUser", JavaConverters.asScalaBuffer(List.of(id))),
                    2 * 60 * 1000,
                    10 * 1000
            );
            String userString = Await.result(future, Duration.Inf()).toString();
            System.out.println("getUser: " + userString);
            Gson g = new Gson();
            return g.fromJson(userString, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Page<User> listUser(int pageNo) {
        try {
            System.out.println("listUser");
            scala.concurrent.Future future = client.call(
                    p.call("listUser", JavaConverters.asScalaBuffer(List.of(pageNo))),
                    2 * 60 * 1000,
                    10 * 1000
            );
            String pageUserString = Await.result(future, Duration.Inf()).toString();
            Page<User> user = new Page<User>();
            Type type = new TypeToken<Page<User>>(){}.getType();
            Gson g = new Gson();
            return g.fromJson(pageUserString, type);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
    }
}
