package benchmark.rpc;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import io.github.free.lock.pcprpc.PcpRpc;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import scala.collection.JavaConverters;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import scala.compat.java8.FutureConverters;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

@State(Scope.Benchmark)
public class Client extends AbstractClient {

    public static final int CONCURRENCY = 32;
    private final UserServicePCPClientImpl userService;
    private final PcpRpc.ClientPool rpcClient;

    public Client() {
       this.rpcClient = PcpRpc.getPCClientPool(
               () -> FutureConverters.toScala(
                       CompletableFuture.supplyAsync(() -> {
                           return new PcpRpc.ServerAddress("benchmark-server", 8080);
                       })
               ),
               PcpRpc.defGenerateSandbox(),
               120,
               8,
               ExecutionContext.global()
       );
       this.userService = new UserServicePCPClientImpl(this.rpcClient);
    }

    @Override
    protected UserService getUserService() {
        return userService;
    }

    @TearDown
    public void close() throws IOException {
        this.rpcClient.clean();
    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public boolean existUser() throws Exception {
        return super.existUser();
    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public boolean createUser() throws Exception {
        return super.createUser();
    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public User getUser() throws Exception {
        return super.getUser();
    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Override
    public Page<User> listUser() throws Exception {
        return super.listUser();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();

        for (int i = 0; i < 60; i++) {
            try {
                System.out.println(client.getUser());
                break;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }

        client.close();

        Options opt = new OptionsBuilder()//
                .include(Client.class.getSimpleName())//
                .warmupIterations(3)//
                .warmupTime(TimeValue.seconds(10))//
                .measurementIterations(3)//
                .measurementTime(TimeValue.seconds(10))//
                .threads(CONCURRENCY)//
                .forks(1)//
                .build();

        new Runner(opt).run();
    }
}