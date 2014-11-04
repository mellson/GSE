using ApproximatorClient.Sensors;

namespace ApproximatorClient
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow
    {
        public MainWindow()
        {
            InitializeComponent();
            new MouseSensor("User1");
            new KeyboardSensor("User1");
        }
    }
}
