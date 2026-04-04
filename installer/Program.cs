using System.Diagnostics;
using System.IO.Compression;
using System.Reflection;
using System.Windows.Forms;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        ApplicationConfiguration.Initialize();

        try
        {
            string installRoot = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "SunDirectionApp");
            string tempZip = Path.Combine(Path.GetTempPath(), "SunDirectionApp-install.zip");

            Directory.CreateDirectory(Path.GetDirectoryName(tempZip)!);
            ExtractEmbeddedZip(tempZip);

            if (Directory.Exists(installRoot))
            {
                Directory.Delete(installRoot, recursive: true);
            }

            ZipFile.ExtractToDirectory(tempZip, Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), overwriteFiles: true);
            File.Delete(tempZip);

            string appExe = Path.Combine(installRoot, "SunDirectionApp.exe");
            if (!File.Exists(appExe))
            {
                throw new FileNotFoundException("Installed executable was not found.", appExe);
            }

            CreateShortcut(
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), "Sun Direction App.lnk"),
                appExe,
                installRoot);

            CreateShortcut(
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Programs), "Sun Direction App.lnk"),
                appExe,
                installRoot);

            MessageBox.Show(
                "Sun Direction App 설치가 완료되었습니다.",
                "설치 완료",
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);

            Process.Start(new ProcessStartInfo
            {
                FileName = appExe,
                WorkingDirectory = installRoot,
                UseShellExecute = true,
            });
        }
        catch (Exception ex)
        {
            MessageBox.Show(
                ex.Message,
                "설치 실패",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }
    }

    private static void ExtractEmbeddedZip(string destinationPath)
    {
        Assembly assembly = Assembly.GetExecutingAssembly();
        using Stream? resourceStream = assembly.GetManifestResourceStream("SunDirectionApp.zip");
        if (resourceStream is null)
        {
            throw new InvalidOperationException("Embedded application package was not found.");
        }

        using FileStream fileStream = File.Create(destinationPath);
        resourceStream.CopyTo(fileStream);
    }

    private static void CreateShortcut(string shortcutPath, string targetPath, string workingDirectory)
    {
        Type? shellType = Type.GetTypeFromProgID("WScript.Shell");
        if (shellType is null)
        {
            throw new InvalidOperationException("Windows shortcut shell is unavailable.");
        }

        dynamic shell = Activator.CreateInstance(shellType)!;
        dynamic shortcut = shell.CreateShortcut(shortcutPath);
        shortcut.TargetPath = targetPath;
        shortcut.WorkingDirectory = workingDirectory;
        shortcut.IconLocation = targetPath + ",0";
        shortcut.Save();
    }
}