using System;
using System.Media;
using System.Threading;
using System.Windows;
using System.Windows.Media;
using ApproximatorClient.Sensors;
using log4net;
using WebSocketSharp;

namespace ApproximatorClient
{
    /// <summary>
    ///     Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow
    {
        private static readonly ILog PromptLog = LogManager.GetLogger("PromptLogger");
        private static readonly ILog DataLog = LogManager.GetLogger("DataLogger");

        public MainWindow()
        {
            InitializeComponent();
            var ws = new WebSocket("ws://spcl.cloudapp.net:6696/users");
            ws.OnMessage += (sender, e) => DataLog.Info(@"Response from WebSocket" + e.Data);
            ws.Connect();

            // We can communicate back with the server if we need it
            ws.Send("Hello From .NET Client");

            var userName = Environment.UserName;
            new MouseSensor(userName);
            new KeyboardSensor(userName);
            new FaceDetectionSensor(userName, cameraIndex: 0);
            var visualBellThread = new Thread(VisualBell) {IsBackground = true};
            visualBellThread.Start();
        }

        public void VisualBell()
        {
            var sound = new SoundPlayer(@"Resources\Notify.wav");
            var random = new Random();
            while (true)
            {
                Dispatcher.Invoke(new Action(() => Top = 0));
                Dispatcher.Invoke(new Action(() => Left = SystemParameters.PrimaryScreenWidth - Width));
                Dispatcher.Invoke(new Action(() => Topmost = true));

                for (var i = 0; i <= 20; i++)
                {
                    Dispatcher.Invoke(i%2 == 0
                        ? (() => indicator.Fill = new SolidColorBrush(Colors.Black))
                        : new Action(() => indicator.Fill = new SolidColorBrush(Colors.White)));
                    if (i%10 == 0)
                        sound.Play();
                    Thread.Sleep(200);
                }

                Dispatcher.Invoke(new Action(() => Topmost = false));
                PromptLog.Info("Prompted user");
                Thread.Sleep(random.Next(60000*20, 60000*40)); //wait between 20-40 minutes
            }
        }
    }
}
