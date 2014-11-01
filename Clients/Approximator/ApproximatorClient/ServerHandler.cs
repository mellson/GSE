using System;

namespace ApproximatorClient
{
    class ServerHandler
    {
        // Upload to infrastructure and return true if the upload was successfull
        public static bool UploadJson(string json)
        {
            Console.Out.WriteLine(json);
            return true;
        }
    }
}
