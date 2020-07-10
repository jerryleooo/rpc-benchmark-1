package benchmark.rpc;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import io.github.free.lock.pcp.PcpClient;
import io.github.free.lock.pcprpc.PcpRpc;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            CompletableFuture jFuture = (CompletableFuture)(FutureConverters.toJava(
                    client.call(
                            p.call("existUser", JavaConverters.asScalaBuffer(List.of(email))),
                            2 * 60 * 1000,
                            10 * 1000
                    )
            ));
            return (Boolean) jFuture.get();
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean createUser(User user) {
        try {
            CompletableFuture jFuture = (CompletableFuture)(FutureConverters.toJava(
                    client.call(
                            p.call("createUser", JavaConverters.asScalaBuffer(List.of(JSON.stringify(user, (Object data, Stack<String> path) -> null)))),
                            2 * 60 * 1000,
                            10 * 1000
                    )
            ));
            return (Boolean) jFuture.get();
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public User getUser(long id) {
        try {
            CompletableFuture jFuture = (CompletableFuture)(FutureConverters.toJava(
                    client.call(
                            p.call("getUser", JavaConverters.asScalaBuffer(List.of(id))),
                            2 * 60 * 1000,
                            10 * 1000
                    )
            ));
            return JSON.parseTo(jFuture.get());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Page<User> listUser(int pageNo) {
        try {
            CompletableFuture jFuture = (CompletableFuture)(FutureConverters.toJava(
                    client.call(
                            p.call("listUser", JavaConverters.asScalaBuffer(List.of(pageNo))),
                            2 * 60 * 1000,
                            10 * 1000
                    )
            ));
            return (Page<User>)jFuture.get();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
    }
}
