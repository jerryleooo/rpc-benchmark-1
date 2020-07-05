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
import scala.collection.JavaConverters;
import scala.concurrent.Await;


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
            return Await.result(
                    client.call(
                            p.call("existUser", JavaConverters.asScalaBuffer(List.of(email))),
                            2 * 60 * 1000,
                            10 * 1000
                    ),
                    scala.concurrent.duration.Duration.create("120 seconds")
            );
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean createUser(User user) {
        return false;
    }

    @Override
    public User getUser(long id) {
        return null;
    }

    @Override
    public Page<User> listUser(int pageNo) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
