package cl.duoc.vetcontrol.agenda.client;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.Map;
@FeignClient(name="veterinario-service") public interface VeterinarioClient { @GetMapping("/api/v1/veterinarios/{id}") Map<String,Object> findById(@PathVariable("id") Long id); }
