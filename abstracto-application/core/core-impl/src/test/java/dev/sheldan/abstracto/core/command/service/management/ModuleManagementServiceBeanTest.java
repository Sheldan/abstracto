package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.AModule;
import dev.sheldan.abstracto.core.command.repository.ModuleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModuleManagementServiceBeanTest {

    @InjectMocks
    private ModuleManagementServiceBean unitToTest;

    @Mock
    private ModuleRepository repository;

    private static final String MODULE_NAME = "module";

    @Test
    public void doesModuleExist() {
        AModule aModule = aModule();
        when(repository.findByName(MODULE_NAME)).thenReturn(aModule);

        assertThat(unitToTest.doesModuleExist(MODULE_NAME)).isTrue();
    }

    @Test
    public void doesModuleNotExist() {
        when(repository.findByName(MODULE_NAME)).thenReturn(null);

        assertThat(unitToTest.doesModuleExist(MODULE_NAME)).isFalse();
    }

    @Test
    public void findModuleByName() {
        AModule aModule = aModule();
        when(repository.findByName(MODULE_NAME)).thenReturn(aModule);

        assertThat(unitToTest.findModuleByName(MODULE_NAME)).isEqualTo(aModule);
    }

    @Test
    public void createModule() {
        ArgumentCaptor<AModule> aModuleArgumentCaptor = ArgumentCaptor.forClass(AModule.class);

        unitToTest.createModule(MODULE_NAME);

        verify(repository).save(aModuleArgumentCaptor.capture());
        AModule aModule = aModuleArgumentCaptor.getValue();
        assertThat(aModule.getName()).endsWith(MODULE_NAME);
    }

    @Test
    public void getOrCreate() {
        AModule aModule = aModule();
        when(repository.findByName(MODULE_NAME)).thenReturn(aModule);

        AModule createdModule = unitToTest.getOrCreate(MODULE_NAME);

        assertThat(createdModule).isEqualTo(aModule);
    }

    @Test
    public void getOrCreateNotExisting() {
        AModule aModule = aModule();
        when(repository.findByName(MODULE_NAME)).thenReturn(null);
        ArgumentCaptor<AModule> aModuleArgumentCaptor = ArgumentCaptor.forClass(AModule.class);
        when(repository.save(any())).thenReturn(aModule);

        AModule returnedModule = unitToTest.getOrCreate(MODULE_NAME);

        verify(repository).save(aModuleArgumentCaptor.capture());
        AModule createdModule = aModuleArgumentCaptor.getValue();
        assertThat(createdModule.getName()).endsWith(MODULE_NAME);
        assertThat(returnedModule).isEqualTo(aModule);
    }

    private AModule aModule() {
        return AModule
                .builder()
                .build();
    }
}
