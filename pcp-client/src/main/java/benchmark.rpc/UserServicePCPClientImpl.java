package benchmark.rpc;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;

import java.io.Closeable;
import java.io.IOException;

public class UserServicePCPClientImpl implements UserService, Closeable {

    private final String host = "benchmark-server";
    private final int port = 8080;

    @Override
    public boolean existUser(String email) {
        return false;
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
