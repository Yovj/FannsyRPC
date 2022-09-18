import com.google.common.util.concurrent.RateLimiter;
import com.yf.FannsyRPCApplication;
import com.yf.Robot;
import com.yf.common.extension.ExtensionLoader;
import com.yf.common.factory.SingletonFactory;
import com.yf.compress.Compress;
import com.yf.compress.gzip.GzipCompress;
import com.yf.config.RpcServiceConfig;
import com.yf.example.ExampleClient;
import com.yf.proxy.RpcClientProxy;
import com.yf.remoting.dto.RpcResponse;
import com.yf.serialize.Serializer;
import com.yf.serialize.kyro.KyroSerializer;
import com.yf.service.HelloService;
import com.yf.service.impl.HelloServiceImpl;
import com.yf.transport.RpcRequestTransport;
import com.yf.transport.client.NettyRpcClient;
import com.yf.transport.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 15:38
 * @version: 1.0.0
 * @url:
 */
@Slf4j
@SpringBootTest(classes = FannsyRPCApplication.class)
@RunWith(SpringRunner.class)
public class test {

    @Test
    public void testSerialize(){
//        RpcRequest request = RpcRequest.builder()
//                .requestId("12306")
//                .methodName("test method")
//                .build();
//
//        KyroSerializer kyroSerializer = new KyroSerializer();
//        byte[] serialize = kyroSerializer.serialize(request);
//        System.out.println(serialize);
//
//        KyroSerializer kyroSerializer1 = new KyroSerializer();
//        Object deserialize = kyroSerializer1.deserialize(serialize,RpcRequest.class);
//        System.out.println(deserialize);

        RpcResponse<Object> abc = RpcResponse.success(null, "abc");
        Serializer serializer = new KyroSerializer();
        byte[] serialize = serializer.serialize(abc);
        Serializer serializer1 = new KyroSerializer();
        RpcResponse deserialize = serializer1.deserialize(serialize, RpcResponse.class);
        System.out.println(deserialize);

    }

    @Test
    public void testCurator() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                .connectString("192.168.64.129")
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        zkClient.create().forPath("/node2","java".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = zkClient.getData().forPath("/node2");
        System.out.println(bytes);
    }

    @Test
    public void testZip() throws IOException {
        Compress compress = new GzipCompress();
        byte[] bytes = "javaabchahahahhahahasdjfhsjkhncoscooasnflvnalbfhacjklnaklnscklnlajkbjklcbjkab vmls dlvbnljashbvlakl;vc#^#%&$fvhuihinadfllhahhahahahahha".getBytes();
        System.out.println(bytes.length);

        byte[] compressd = compress.compress(bytes);
        System.out.println(compressd.length);
        System.out.println(compressd);
        byte[] decompress = compress.decompress(compressd);
        System.out.println(new String(decompress));


    }

    @Test
    public void testServerRegiste(){
        HelloService helloService = new HelloServiceImpl();
        HashSet<String> permitIps = new HashSet<>();
        permitIps.add("192.168.64.129");
        permitIps.add("100.80.185.154");

        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test1")
                .version("version1")
                .service(helloService)
                .token("token-a")
//                .permitIps(permitIps)
                .build();
        // 手工注册服务

        NettyRpcServer server = SingletonFactory.getInstance(NettyRpcServer.class);
        server.registerService(rpcServiceConfig);
        try {
            server.start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testClientDiscovery() throws IOException {
        ArrayList<String> directIps = new ArrayList<>();
//        directIps.add("192.168.64.129");

        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setGroup("test1");
        rpcServiceConfig.setVersion("version1");
        rpcServiceConfig.setToken("token-a");
        rpcServiceConfig.setDirectIp(directIps);
        RpcRequestTransport rpcRequestTransport = SingletonFactory.getInstance(NettyRpcClient.class);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcServiceConfig,rpcRequestTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        helloService.sayHello();
        System.out.println("yyyyy");
        System.in.read();


    }

    @Test
    public void testSPI(){
        ServiceLoader<Robot> load = ServiceLoader.load(Robot.class);
        load.forEach(Robot::sayHello);
    }

    @Test
    public void testFannsySPI(){
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer kyro = extensionLoader.getExtension("kyro");
        System.out.println(kyro);

    }

    @Test
    public void testBalance(){
//        List<String> serviceUrlList = new ArrayList<>();
//        serviceUrlList.add("192.168.64.129:100");
//        serviceUrlList.add("192.168.64.130:100");
//        serviceUrlList.add("192.168.64.131:100");
//        serviceUrlList.add("192.168.64.132:100");
//        serviceUrlList.add("192.168.64.133:100");
//        System.out.println(serviceUrlList);
//        HelloService helloService = new HelloServiceImpl();
//        String param = "ac";
//        RpcRequest rpcRequest = RpcRequest.builder().requestId("abc").parameters(new Object[]{param}).methodName("helloService").build();
//
//        ConsistentHashLoadBalance consistentHashLoadBalance = new ConsistentHashLoadBalance();
//        String s = consistentHashLoadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
//        System.out.println(s);

    }

    private static volatile RateLimiter rateLimiter = RateLimiter.create(5);

    @Test
    public void testLimit() throws InterruptedException {
        Thread[] threads = new Thread[15];

        for (int i = 0; i < 15; i++) {
            threads[i] = new Thread(() -> {
                if (rateLimiter.tryAcquire()){
                    System.out.println("i am in");
                }
                else {
                    System.out.println("out .........");
                }
            });
        }

        for (int i = 0; i < 15; i++) {
            Thread.sleep(100);
            threads[i].start();
        }




    }



    @Resource
    private HelloService helloService;

    @Resource
    private ExampleClient exampleClient;
    @Test
    public void testAnnotaionServer(){
        System.out.println(helloService);
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Test
    public void testAnnotaionClient(){
        exampleClient.testClient();
    }






}
