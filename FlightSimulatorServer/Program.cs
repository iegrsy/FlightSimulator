using System;
using System.Threading.Tasks;
using FlightSimulator;
using Grpc.Core;

namespace FlightSimulatorServer
{
    public class FlightSimulatorSeviceImpl : FlightSimulatorService.FlightSimulatorServiceBase
    {
        public FlightSimulatorSeviceImpl() { }

        public override async Task GetCameraStream(DummyQ request, IServerStreamWriter<CameraStreamQ> responseStream, ServerCallContext context)
        {
            int count = 0;
            while (true)
            {
                if (count > 1000)
                    break;

                var stream = new CameraStream() { Data = Google.Protobuf.ByteString.CopyFromUtf8("Count: " + count) };
                var streamQ = new CameraStreamQ();
                streamQ.Streams.Add(stream);

                await responseStream.WriteAsync(streamQ);

                count++;
            }
        }
    }

    class Program
    {
        static void Main(string[] args)
        {
            string _host = "localhost";
            int _port = 8888;

            Server server = new Server
            {
                Services = { FlightSimulatorService.BindService(new FlightSimulatorSeviceImpl()) },
                Ports = { new ServerPort(_host, _port, ServerCredentials.Insecure) }
            };
            server.Start();

            Console.WriteLine("Server listening on port " + _port);
            Console.WriteLine("Press any key to stop the server...");
            Console.ReadKey();

            server.ShutdownAsync().Wait();
        }
    }
}
