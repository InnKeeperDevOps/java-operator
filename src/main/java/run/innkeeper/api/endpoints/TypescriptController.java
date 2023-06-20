package run.innkeeper.api.endpoints;

import cz.habarta.typescript.generator.ClassMapping;
import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.JsonLibrary;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeScriptFileType;
import cz.habarta.typescript.generator.TypeScriptGenerator;
import cz.habarta.typescript.generator.TypeScriptOutputKind;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.innkeeper.api.dto.SimpleExtensionDTO;
import run.innkeeper.utilities.Logging;

@RestController
@RequestMapping("/ts")
public class TypescriptController{
  TypeScriptGenerator typeScriptGenerator;

  public TypescriptController() {
    final Settings settings = new Settings();
    settings.outputKind = TypeScriptOutputKind.global;
    settings.jsonLibrary = JsonLibrary.jackson2;
    settings.noFileComment = true;
    settings.noTslintDisable = true;
    settings.noEslintDisable = true;
    settings.newline = "\n";
    settings.classLoader = Thread.currentThread().getContextClassLoader();
    settings.outputFileType = TypeScriptFileType.implementationFile;
    settings.mapClasses = ClassMapping.asClasses;
    settings.customTypeNaming.put("run.innkeeper.v1.deployment.crd.DeploymentSpec", "GuestDeploymentSpec");
    settings.customTypeNaming.put("run.innkeeper.v1.deployment.crd.DeploymentStatus", "GuestDeploymentStatus");
    settings.customTypeNaming.put("run.innkeeper.v1.service.crd.ServiceSpec", "GuestServiceSpec");
    settings.customTypeNaming.put("run.innkeeper.v1.service.crd.ServiceStatus", "GuestServiceStatus");
    settings.customTypeNaming.put("run.innkeeper.v1.guest.crd.objects.service.ServicePort", "GuestServicePort");
    settings.customTypeNaming.put("run.innkeeper.v1.guest.crd.objects.deployment.Container", "GuestContainer");
    typeScriptGenerator = new TypeScriptGenerator(settings);

  }

  @GetMapping("/all")
  public String getAllDTOs() {
    return typeScriptGenerator.generateTypeScript(
        Input.from(
            new Reflections(
                "run.innkeeper.api.dto",
                new SubTypesScanner(false)
            ).getSubTypesOf(Object.class).toArray(new Class[0])
        )
    );
  }
}
