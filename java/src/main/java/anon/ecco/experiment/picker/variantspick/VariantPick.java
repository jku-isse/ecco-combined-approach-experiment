package anon.ecco.experiment.picker.variantspick;

import anon.ecco.experiment.utils.vevos.VevosUtils;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class VariantPick {

    @Getter
    private List<Path> variantPickPaths;
    @Getter
    private List<String> variantPickConfigurations;

    public VariantPick(Path variantsBaseDir, int numberOfVariants){
        List<Path> variantPaths = VevosUtils.getVariantFolders(variantsBaseDir);
        this.variantPickPaths = this.pickRandomVariants(variantPaths, numberOfVariants);
        this.setVariantConfigurations();
    }

    private List<Path> pickRandomVariants(List<Path> variantPaths, int pickSize){
        Collections.shuffle(variantPaths);
        return variantPaths.subList(0, pickSize);
    }

    private void setVariantConfigurations(){
        List<String> configList = new LinkedList<>();
        for (Path variantPath : this.variantPickPaths){
            configList.add(VevosUtils.variantPathToConfigString(variantPath));
        }
        this.variantPickConfigurations = configList;
    }

    @Override
    public String toString(){
        return String.join("; ", this.variantPickConfigurations);
    }
}
